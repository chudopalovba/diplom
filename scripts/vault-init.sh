#!/bin/sh
set -e

echo "🔐 Waiting for Vault to be ready..."
sleep 5

echo "📝 Creating policies..."
vault policy write gitlab-ci /vault/policies/gitlab-ci.hcl || echo "Policy already exists"

echo "🔑 Enabling AppRole auth method..."
vault auth enable approle 2>/dev/null || echo "AppRole already enabled"

echo "📋 Creating GitLab CI role..."
vault write auth/approle/role/gitlab-ci \
    token_ttl=1h \
    token_max_ttl=4h \
    policies="gitlab-ci" \
    secret_id_ttl=0

echo "🆔 Getting RoleID and SecretID..."
ROLE_ID=$(vault read -field=role_id auth/approle/role/gitlab-ci/role-id)
SECRET_ID=$(vault write -f -field=secret_id auth/approle/role/gitlab-ci/secret-id)

echo ""
echo "================================================"
echo "✅ Vault initialized successfully!"
echo "================================================"
echo "Vault Address: http://vault:8200"
echo "Root Token: root-token-dev"
echo ""
echo "Add these to GitLab CI/CD Variables:"
echo "  VAULT_ADDR = http://vault:8200"
echo "  VAULT_ROLE_ID = $ROLE_ID"
echo "  VAULT_SECRET_ID = $SECRET_ID (masked)"
echo "================================================"

echo ""
echo "🗂️ Creating KV v2 secrets engine..."
vault secrets enable -path=secret kv-v2 2>/dev/null || echo "KV engine already enabled"

echo "📦 Creating secrets for nodes/credentials..."

# SSH credentials для деплоя
vault kv put secret/nodes/credentials/ssh-key \
    private_key="-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAABlwAAAAdzc2gtcn
NhAAAAAwEAAQAAAYEA1234567890abcdefghijklmnopqrstuvwxyz
YOUR_ACTUAL_SSH_PRIVATE_KEY_CONTENT_HERE
-----END OPENSSH PRIVATE KEY-----" \
    public_key="ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQD... your-key@example.com" \
    username="deploy" \
    host="192.168.0.102"

# SSH password (альтернатива)
vault kv put secret/nodes/credentials/ssh-pass \
    username="deploy" \
    password="your-secure-password" \
    host="192.168.0.102"

# Nexus credentials
vault kv put secret/nexus/credentials \
    username="admin" \
    password="admin123" \
    registry="192.168.0.102:5001"

# Database credentials
vault kv put secret/database/credentials \
    db_name="devops_platform" \
    db_user="postgres" \
    db_password="postgres" \
    db_host="postgres" \
    db_port="5432"

# GitLab API token
vault kv put secret/gitlab/api \
    token="glpat-GMw-1xhdwsi3nLP8R6AZ" \
    url="http://gitlab.local:8929"

# SonarQube token
vault kv put secret/sonarqube/token \
    token="squ_your_token_here" \
    url="http://sonarqube:9000"

echo "✅ All secrets created!"
echo ""
echo "📖 List secrets:"
echo "   vault kv list secret/nodes/credentials"
echo ""
echo "🔍 Read secret:"
echo "   vault kv get secret/nodes/credentials/ssh-key"
echo ""
echo "🎉 Vault setup complete!"