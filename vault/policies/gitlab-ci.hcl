# Полный доступ к секретам нод
path "secret/data/nodes/*" {
  capabilities = ["read", "list"]
}

path "secret/metadata/nodes/*" {
  capabilities = ["list"]
}

# Доступ к Nexus credentials
path "secret/data/nexus/*" {
  capabilities = ["read", "list"]
}

# Доступ к Database credentials
path "secret/data/database/*" {
  capabilities = ["read", "list"]
}

# Доступ к GitLab credentials
path "secret/data/gitlab/*" {
  capabilities = ["read", "list"]
}

# Доступ к SonarQube credentials
path "secret/data/sonarqube/*" {
  capabilities = ["read", "list"]
}

# Список всех секретов
path "secret/metadata/*" {
  capabilities = ["list"]
}