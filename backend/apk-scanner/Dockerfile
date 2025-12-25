# Base Image for Python 3.10
FROM python:3.10-slim

# Install system dependencies (gcc for compiling some ML libs)
RUN apt-get update && apt-get install -y \
    gcc \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy requirements file (Modify if your path differs)
# We assume the Docker context is the service directory
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application code
COPY . .

# Expose port (8088 as per start_all_single.ps1)
EXPOSE 8088

# Run the application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8088"]
