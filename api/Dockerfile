FROM public.ecr.aws/lambda/python:3.12

WORKDIR ${LAMBDA_TASK_ROOT}

RUN pip install --no-cache-dir \
    "numpy==1.26.4" \
    "scipy==1.11.4" \
    "scikit-learn==1.4.2" \
    "joblib==1.3.2"

COPY lambda_handler.py .
COPY models/sast_pipeline.joblib /opt/ml/model/sast_pipeline.joblib

CMD ["lambda_handler.handler"]