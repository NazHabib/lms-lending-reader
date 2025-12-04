#!/usr/bin/env bash
set -euxo pipefail

# Basics (including netcat for your pipeline checks)
apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y \
  apt-transport-https ca-certificates curl gnupg lsb-release \
  openjdk-17-jre-headless maven netcat-openbsd

# Docker (official repository)
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
 | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
https://download.docker.com/linux/ubuntu ${UBUNTU_CODENAME} stable" \
> /etc/apt/sources.list.d/docker.list
apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y \
  docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Create your deployment user and allow docker without sudo
id -u nazmu &>/dev/null || adduser --disabled-password --gecos "" nazmu
usermod -aG docker nazmu

# Make Docker start on boot
systemctl enable --now docker
