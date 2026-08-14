import pandas as pd
import joblib
import os
import re
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline

# ── Tokenizer ────────────────────────────────────────────────────────────────
def tokenize(code):
    code = re.sub(r"/\*.*?\*/", " ", code, flags=re.DOTALL)
    code = re.sub(r"(//|#)[^\n]*", " ", code)
    code = re.sub(r'"(?:[^"\\]|\\.)*"', " STRING_LITERAL ", code)
    code = re.sub(r"'(?:[^'\\]|\\.)*'", " STRING_LITERAL ", code)
    tokens = re.findall(r"[A-Za-z_][A-Za-z0-9_]*", code)
    expanded = []
    for tok in tokens:
        for sub in re.sub(r"([a-z])([A-Z])", r"\1 \2", tok).split():
            sub = sub.lower()
            if len(sub) >= 2:
                expanded.append(sub)
    return " ".join(expanded)

# ── Load dataset ─────────────────────────────────────────────────────────────
print("Loading dataset...")

# Check dataset.csv exists
if not os.path.exists("dataset.csv"):
    print("ERROR: dataset.csv not found in current folder")
    print("Current folder:", os.getcwd())
    print("Please copy dataset.csv here first")
    exit(1)

df = pd.read_csv("dataset.csv")
print(f"Loaded {len(df)} samples")
print(df["label"].value_counts())

X = df["code_snippet"].fillna("").apply(tokenize)
y = df["label"]

# ── Build pipeline ────────────────────────────────────────────────────────────
pipeline = Pipeline([
    ("tfidf", TfidfVectorizer(
        ngram_range=(1, 3),
        max_features=10000,
        sublinear_tf=True,
        min_df=2,
    )),
    ("rf", RandomForestClassifier(
        n_estimators=200,
        class_weight="balanced",
        random_state=42,
        n_jobs=-1,
    )),
])

# ── Train ─────────────────────────────────────────────────────────────────────
print("\nTraining model...")
pipeline.fit(X, y)
print("Training complete!")
print(f"Classes: {pipeline.classes_}")

# ── Save ──────────────────────────────────────────────────────────────────────
os.makedirs("api/models", exist_ok=True)
joblib.dump(pipeline, "api/models/sast_pipeline.joblib")

size_mb = os.path.getsize("api/models/sast_pipeline.joblib") / (1024 * 1024)
print(f"\nModel saved → api/models/sast_pipeline.joblib ({size_mb:.1f} MB)")
print("Ready to build Docker image!")