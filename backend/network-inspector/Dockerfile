# Base Image
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Copy requirements
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy app code
COPY . .

# Expose port 8087
EXPOSE 8087

# Run app
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8087"]
