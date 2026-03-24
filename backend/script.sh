#!/bin/bash
cd /home/bogdan/diplom/backend/src/main/resources/templates/ansible

# Общие файлы
cat > ansible.cfg << 'EOF'
[defaults]
inventory = inventory.ini
host_key_checking = False
timeout = 30
EOF

cat > inventory.ini << 'EOF'
[app]
# deploy-server ansible_host=YOUR_SERVER_IP ansible_user=deploy
EOF

# 18 комбинаций
backends=("java" "python" "csharp")
frontends=("react" "vue" "angular")
dockers=("docker" "no-docker")

for b in "${backends[@]}"; do
  for f in "${frontends[@]}"; do
    for d in "${dockers[@]}"; do
      dir="${b}-${f}-${d}"
      mkdir -p "${dir}/roles/deploy/tasks"
      mkdir -p "${dir}/roles/deploy/handlers"
      mkdir -p "${dir}/roles/deploy/defaults"

      cat > "${dir}/playbook.yml" << PLAYEOF
---
- name: Deploy ${b} + ${f} application (${d})
  hosts: app
  become: yes
  roles:
    - deploy
PLAYEOF

      cat > "${dir}/roles/deploy/defaults/main.yml" << DEFEOF
---
# Default variables for ${b}-${f}-${d} deploy
DEFEOF

      cat > "${dir}/roles/deploy/tasks/main.yml" << TASKEOF
---
# Deploy tasks for ${b}-${f}-${d}
TASKEOF

      cat > "${dir}/roles/deploy/handlers/main.yml" << HANDEOF
---
# Handlers for ${b}-${f}-${d}
HANDEOF

      echo "Created: ${dir}"
    done
  done
done
