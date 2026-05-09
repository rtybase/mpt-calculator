FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# ---------------------------------------------------------
# Base system setup
# ---------------------------------------------------------
RUN apt-get update && apt-get install -y \
    software-properties-common \
    curl \
    wget \
    gnupg \
    ca-certificates \
    build-essential \
    dos2unix \
    python3 \
    python3-venv \
    python3-pip \
    python3-dev

# ---------------------------------------------------------
# Create Python virtual environment
# ---------------------------------------------------------
RUN python3 -m venv /opt/python/venv

# Ensure venv Python & pip are used by default
ENV PATH="/opt/python/venv/bin:$PATH"

# Upgrade pip inside the venv
RUN pip install --no-cache-dir --upgrade pip

# ---------------------------------------------------------
# Install OpenJDK 25 (Azul Zulu)
# ---------------------------------------------------------
RUN wget -qO - https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" \
        > /etc/apt/sources.list.d/zulu.list && \
    apt-get update && apt-get install -y zulu25-jdk

ENV JAVA_HOME=/usr/lib/jvm/zulu25
ENV PATH="$JAVA_HOME/bin:$PATH"

# ---------------------------------------------------------
# Install Python dependencies inside the venv
# ---------------------------------------------------------
RUN pip install --no-cache-dir \
    pandas \
    joblib \
    holidays \
    setuptools \
    mysql-connector-python \
    yfinance

# ---------------------------------------------------------
# Cleanup
# ---------------------------------------------------------
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app

CMD ["/bin/bash"]