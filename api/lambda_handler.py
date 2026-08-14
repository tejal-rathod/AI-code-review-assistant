import json
import os
import re
import joblib
import numpy as np

MODEL_PATH = os.environ.get("MODEL_PATH", "/opt/ml/model/sast_pipeline.joblib")
CONFIDENCE_THRESHOLD = float(os.environ.get("CONFIDENCE_THRESHOLD", "0.35"))
WINDOW_SIZE = int(os.environ.get("WINDOW_SIZE", "10"))
STEP_SIZE   = int(os.environ.get("STEP_SIZE", "4"))

_pipeline = None
_classes  = None

def _load_model():
    global _pipeline, _classes
    if _pipeline is None:
        _pipeline = joblib.load(MODEL_PATH)
        _classes  = _pipeline.classes_
    return _pipeline, _classes

def _remove_block_comments(code):
    return re.sub(r"/\*.*?\*/", " ", code, flags=re.DOTALL)

def _remove_single_line_comments(code):
    return re.sub(r"(//|#)[^\n]*", " ", code)

def _remove_string_literals(code):
    code = re.sub(r'"(?:[^"\\]|\\.)*"', " STRING_LITERAL ", code)
    code = re.sub(r"'(?:[^'\\]|\\.)*'", " STRING_LITERAL ", code)
    return code

def _split_camel_case(token):
    return re.sub(r"([a-z])([A-Z])", r"\1 \2", token)

def tokenize(code: str) -> str:
    code = _remove_block_comments(code)
    code = _remove_single_line_comments(code)
    code = _remove_string_literals(code)
    tokens = re.findall(r"[A-Za-z_][A-Za-z0-9_]*", code)
    expanded = []
    for tok in tokens:
        for sub in _split_camel_case(tok).split():
            sub = sub.lower()
            if len(sub) >= 2:
                expanded.append(sub)
    return " ".join(expanded)

SEVERITY_MAP = {
    "sql_injection":    "CRITICAL",
    "hardcoded_secret": "CRITICAL",
    "insecure_api":     "HIGH",
    "safe":             "SAFE",
}

REMEDIATION_MAP = {
    "sql_injection":    "Use parameterised queries / PreparedStatement. Never concatenate user input into SQL.",
    "hardcoded_secret": "Move secrets to environment variables or AWS Secrets Manager.",
    "insecure_api":     "Enable TLS verification, avoid eval/exec on user input.",
    "safe":             "No action required.",
}

def predict_snippet(code: str) -> dict:
    pipeline, classes = _load_model()
    token_str  = tokenize(code)
    proba      = pipeline.predict_proba([token_str])[0]
    label_idx  = int(np.argmax(proba))
    label      = classes[label_idx]
    confidence = float(proba[label_idx])
    if confidence < CONFIDENCE_THRESHOLD:
        label = "uncertain"
    return {
        "label":         label,
        "confidence":    round(confidence, 4),
        "severity":      SEVERITY_MAP.get(label, "UNKNOWN"),
        "probabilities": {c: round(float(p), 4) for c, p in zip(classes, proba)},
        "remediation":   REMEDIATION_MAP.get(label, "Review manually."),
    }

def scan_file_content(content: str) -> list:
    lines    = content.splitlines()
    findings = []
    for start in range(0, max(1, len(lines) - WINDOW_SIZE + 1), STEP_SIZE):
        end   = min(start + WINDOW_SIZE, len(lines))
        chunk = "\n".join(lines[start:end])
        result = predict_snippet(chunk)
        if result["label"] not in ("safe", "uncertain"):
            if findings and findings[-1]["label"] == result["label"] \
               and abs(int(findings[-1]["line_start"]) - (start + 1)) <= 3:
                continue
            findings.append({
                "line_range":  f"{start + 1}-{end}",
                "line_start":  start + 1,
                "label":       result["label"],
                "severity":    result["severity"],
                "confidence":  result["confidence"],
                "code_chunk":  chunk.strip(),
                "remediation": result["remediation"],
            })
    return findings

def handler(event, context):
    try:
        body = event.get("body", "{}")
        if isinstance(body, str):
            body = json.loads(body)

        mode = body.get("mode", "snippet")

        if mode == "snippet":
            code   = body.get("code", "")
            result = predict_snippet(code)
            payload = {"mode": "snippet", "result": result}

        elif mode == "file":
            content  = body.get("content", "")
            filename = body.get("filename", "unknown.java")
            findings = scan_file_content(content)
            payload  = {
                "mode":          "file",
                "filename":      filename,
                "findings":      findings,
                "finding_count": len(findings),
                "has_critical":  any(f["severity"] == "CRITICAL" for f in findings),
            }

        else:
            return {
                "statusCode": 400,
                "body": json.dumps({"error": f"Unknown mode: {mode}"}),
            }

        return {
            "statusCode": 200,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps(payload),
        }

    except Exception as e:
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)}),
        }