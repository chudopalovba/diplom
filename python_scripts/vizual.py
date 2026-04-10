import matplotlib.pyplot as plt
import numpy as np

# Настройка шрифтов для поддержки кириллицы
plt.rcParams['font.family'] = 'DejaVu Sans'
plt.rcParams['figure.figsize'] = (12, 7)
plt.rcParams['figure.dpi'] = 150

# =============================================================================
# График 1: Сравнение времени развёртывания по итерациям (Docker и Non-Docker)
# =============================================================================

iterations = [1, 2, 3, 4, 5]

# Docker проект
docker_manual = [38, 42, 35, 51, 40]
docker_auto = [9, 8, 9, 8, 9]

# Non-Docker проект
nodocker_manual = [32, 28, 35, 45, 30]
nodocker_auto = [6, 7, 6, 7, 6]

fig, axes = plt.subplots(1, 2, figsize=(14, 6))

# Docker проект
x = np.arange(len(iterations))
width = 0.35

bars1 = axes[0].bar(x - width/2, docker_manual, width, label='Ручной деплой', color='#E57373')
bars2 = axes[0].bar(x + width/2, docker_auto, width, label='Автоматизированный', color='#81C784')

axes[0].set_xlabel('Номер итерации')
axes[0].set_ylabel('Время (минуты)')
axes[0].set_title('Docker-проект (Java + Angular)')
axes[0].set_xticks(x)
axes[0].set_xticklabels(iterations)
axes[0].legend()
axes[0].set_ylim(0, 60)
axes[0].axhline(y=41.2, color='#C62828', linestyle='--', linewidth=1.5, alpha=0.7)
axes[0].axhline(y=8.6, color='#2E7D32', linestyle='--', linewidth=1.5, alpha=0.7)

for bar in bars1:
    height = bar.get_height()
    axes[0].annotate(f'{height}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=9)
for bar in bars2:
    height = bar.get_height()
    axes[0].annotate(f'{height}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=9)

# Non-Docker проект
bars3 = axes[1].bar(x - width/2, nodocker_manual, width, label='Ручной деплой', color='#FFB74D')
bars4 = axes[1].bar(x + width/2, nodocker_auto, width, label='Автоматизированный', color='#64B5F6')

axes[1].set_xlabel('Номер итерации')
axes[1].set_ylabel('Время (минуты)')
axes[1].set_title('Non-Docker проект (Python + React)')
axes[1].set_xticks(x)
axes[1].set_xticklabels(iterations)
axes[1].legend()
axes[1].set_ylim(0, 60)
axes[1].axhline(y=34.0, color='#E65100', linestyle='--', linewidth=1.5, alpha=0.7)
axes[1].axhline(y=6.4, color='#1565C0', linestyle='--', linewidth=1.5, alpha=0.7)

for bar in bars3:
    height = bar.get_height()
    axes[1].annotate(f'{height}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=9)
for bar in bars4:
    height = bar.get_height()
    axes[1].annotate(f'{height}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=9)

plt.tight_layout()
plt.savefig('график_1_сравнение_времени_оба_типа.png', dpi=150, bbox_inches='tight')
plt.show()

# =============================================================================
# График 2: Сравнение средних значений (все 4 варианта)
# =============================================================================

fig, ax = plt.subplots(figsize=(10, 6))

categories = ['Docker\nРучной', 'Docker\nАвтоматизированный', 'Non-Docker\nРучной', 'Non-Docker\nАвтоматизированный']
times = [41.2, 8.6, 34.0, 6.4]
std_devs = [5.9, 0.5, 6.5, 0.5]
colors = ['#E57373', '#81C784', '#FFB74D', '#64B5F6']

bars = ax.bar(categories, times, yerr=std_devs, capsize=5, color=colors, edgecolor='black', linewidth=1.2)

ax.set_ylabel('Время (минуты)')
ax.set_title('Среднее время развёртывания по типам проектов')
ax.set_ylim(0, 55)

for bar, time, std in zip(bars, times, std_devs):
    height = bar.get_height()
    ax.annotate(f'{time} ± {std}', xy=(bar.get_x() + bar.get_width() / 2, height),
                xytext=(0, 5), textcoords="offset points", ha='center', va='bottom', fontsize=11, fontweight='bold')

# Добавление коэффициентов ускорения
ax.annotate('4.8x быстрее', xy=(0.5, 25), fontsize=12, ha='center', color='#2E7D32', fontweight='bold')
ax.annotate('', xy=(1, 10), xytext=(0, 40), arrowprops=dict(arrowstyle='->', color='#2E7D32', lw=2))

ax.annotate('5.3x быстрее', xy=(2.5, 20), fontsize=12, ha='center', color='#1565C0', fontweight='bold')
ax.annotate('', xy=(3, 8), xytext=(2, 32), arrowprops=dict(arrowstyle='->', color='#1565C0', lw=2))

plt.tight_layout()
plt.savefig('график_2_средние_значения.png', dpi=150, bbox_inches='tight')
plt.show()

# =============================================================================
# График 3: Распределение времени по этапам (Docker vs Non-Docker)
# =============================================================================

fig, axes = plt.subplots(2, 2, figsize=(14, 10))

# Docker Ручной
stages_docker_manual = ['Сборка\nбэкенда', 'Сборка\nфронтенда', 'Сборка\nобразов', 'Push в\nregistry', 
                        'SSH +\nнастройка', 'Запуск\nконтейнеров']
times_docker_manual = [8, 5, 10, 4, 6, 8]
axes[0, 0].barh(stages_docker_manual, times_docker_manual, color='#FFCDD2', edgecolor='#C62828')
axes[0, 0].set_xlabel('Время (минуты)')
axes[0, 0].set_title('Docker — Ручной деплой (41.2 мин)')
axes[0, 0].set_xlim(0, 15)
for i, v in enumerate(times_docker_manual):
    axes[0, 0].text(v + 0.3, i, f'{v}', va='center', fontsize=10)

# Docker Автоматизированный
stages_docker_auto = ['git push', 'Build\n(CI/CD)', 'Test', 'Sonar', 'Push', 'Deploy\n(Ansible)']
times_docker_auto = [0.5, 3, 1, 1.5, 1, 1.6]
axes[0, 1].barh(stages_docker_auto, times_docker_auto, color='#C8E6C9', edgecolor='#2E7D32')
axes[0, 1].set_xlabel('Время (минуты)')
axes[0, 1].set_title('Docker — Автоматизированный (8.6 мин)')
axes[0, 1].set_xlim(0, 15)
for i, v in enumerate(times_docker_auto):
    axes[0, 1].text(v + 0.3, i, f'{v}', va='center', fontsize=10)

# Non-Docker Ручной
stages_nodocker_manual = ['Установка\nзависимостей', 'Сборка\nфронтенда', 'SSH\nподключение', 
                          'Копирование\nфайлов', 'Настройка\nокружения', 'Миграции +\nперезапуск']
times_nodocker_manual = [5, 4, 3, 6, 10, 6]
axes[1, 0].barh(stages_nodocker_manual, times_nodocker_manual, color='#FFE0B2', edgecolor='#E65100')
axes[1, 0].set_xlabel('Время (минуты)')
axes[1, 0].set_title('Non-Docker — Ручной деплой (34.0 мин)')
axes[1, 0].set_xlim(0, 15)
for i, v in enumerate(times_nodocker_manual):
    axes[1, 0].text(v + 0.3, i, f'{v}', va='center', fontsize=10)

# Non-Docker Автоматизированный
stages_nodocker_auto = ['git push', 'Build\n(CI/CD)', 'Test', 'Sonar', 'Push\nартефактов', 'Deploy\n(Ansible)']
times_nodocker_auto = [0.5, 2, 0.8, 1.2, 0.7, 1.2]
axes[1, 1].barh(stages_nodocker_auto, times_nodocker_auto, color='#BBDEFB', edgecolor='#1565C0')
axes[1, 1].set_xlabel('Время (минуты)')
axes[1, 1].set_title('Non-Docker — Автоматизированный (6.4 мин)')
axes[1, 1].set_xlim(0, 15)
for i, v in enumerate(times_nodocker_auto):
    axes[1, 1].text(v + 0.3, i, f'{v}', va='center', fontsize=10)

plt.tight_layout()
plt.savefig('график_3_распределение_времени.png', dpi=150, bbox_inches='tight')
plt.show()

# =============================================================================
# График 4: Суммарные затраты в зависимости от частоты деплоя
# =============================================================================

deploys_per_month = np.arange(0, 35, 1)

docker_manual_hours = deploys_per_month * 41.2 / 60
docker_auto_hours = deploys_per_month * 8.6 / 60
nodocker_manual_hours = deploys_per_month * 34.0 / 60
nodocker_auto_hours = deploys_per_month * 6.4 / 60

fig, ax = plt.subplots(figsize=(12, 7))

ax.plot(deploys_per_month, docker_manual_hours, 'r-', linewidth=2, label='Docker — Ручной', marker='o', markevery=5)
ax.plot(deploys_per_month, docker_auto_hours, 'g-', linewidth=2, label='Docker — Автоматизированный', marker='s', markevery=5)
ax.plot(deploys_per_month, nodocker_manual_hours, color='#FF9800', linewidth=2, label='Non-Docker — Ручной', marker='^', markevery=5)
ax.plot(deploys_per_month, nodocker_auto_hours, 'b-', linewidth=2, label='Non-Docker — Автоматизированный', marker='d', markevery=5)

ax.fill_between(deploys_per_month, docker_auto_hours, docker_manual_hours, alpha=0.2, color='green')
ax.fill_between(deploys_per_month, nodocker_auto_hours, nodocker_manual_hours, alpha=0.2, color='blue')

ax.set_xlabel('Количество развёртываний в месяц')
ax.set_ylabel('Суммарное время (часы)')
ax.set_title('Суммарные временные затраты на развёртывание')
ax.legend(loc='upper left')
ax.grid(True, alpha=0.3)
ax.set_xlim(0, 34)
ax.set_ylim(0, 25)

ax.annotate('Docker: экономия 10.8 ч/мес\nNon-Docker: экономия 9.2 ч/мес',
            xy=(20, 13.7), xytext=(24, 18),
            arrowprops=dict(arrowstyle='->', color='black'),
            fontsize=10, bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

plt.tight_layout()
plt.savefig('график_4_суммарные_затраты.png', dpi=150, bbox_inches='tight')
plt.show()

# =============================================================================
# График 5: Сравнение количества операций и точек отказа
# =============================================================================

fig, axes = plt.subplots(1, 2, figsize=(14, 6))

# Количество операций
categories = ['Docker\nРучной', 'Docker\nАвто', 'Non-Docker\nРучной', 'Non-Docker\nАвто']
operations = [13.5, 2, 16, 2]  # средние значения
commands = [20, 1, 25, 1]

x = np.arange(len(categories))
width = 0.35

bars1 = axes[0].bar(x - width/2, operations, width, label='Ручных операций', color='#90CAF9')
bars2 = axes[0].bar(x + width/2, commands, width, label='Вводимых команд', color='#CE93D8')

axes[0].set_ylabel('Количество')
axes[0].set_title('Количество ручных операций и команд')
axes[0].set_xticks(x)
axes[0].set_xticklabels(categories)
axes[0].legend()
axes[0].set_ylim(0, 35)

for bar in bars1:
    height = bar.get_height()
    axes[0].annotate(f'{int(height)}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=10)
for bar in bars2:
    height = bar.get_height()
    axes[0].annotate(f'{int(height)}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=10)

# Точки отказа
failure_points = [9, 2, 12, 2]
colors = ['#E57373', '#81C784', '#FFB74D', '#64B5F6']

bars3 = axes[1].bar(categories, failure_points, color=colors, edgecolor='black', linewidth=1.2)
axes[1].set_ylabel('Количество')
axes[1].set_title('Потенциальные точки отказа')
axes[1].set_ylim(0, 16)

for bar in bars3:
    height = bar.get_height()
    axes[1].annotate(f'{int(height)}', xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3), textcoords="offset points", ha='center', va='bottom', fontsize=11, fontweight='bold')

plt.tight_layout()
plt.savefig('график_5_операции_и_отказы.png', dpi=150, bbox_inches='tight')
plt.show()

# =============================================================================
# График 6: Радарная диаграмма сравнения
# =============================================================================

categories_radar = ['Скорость', 'Повторяемость', 'Надёжность', 
                    'Простота', 'Масштабируемость', 'Аудит']
N = len(categories_radar)

docker_manual_scores = [3, 4, 3, 2, 2, 1]
docker_auto_scores = [9, 10, 8, 9, 9, 10]
nodocker_manual_scores = [4, 3, 3, 2, 2, 1]
nodocker_auto_scores = [10, 10, 8, 9, 9, 10]

angles = [n / float(N) * 2 * np.pi for n in range(N)]
angles += angles[:1]

docker_manual_scores += docker_manual_scores[:1]
docker_auto_scores += docker_auto_scores[:1]
nodocker_manual_scores += nodocker_manual_scores[:1]
nodocker_auto_scores += nodocker_auto_scores[:1]

fig, ax = plt.subplots(figsize=(10, 10), subplot_kw=dict(polar=True))

ax.plot(angles, docker_manual_scores, 'o-', linewidth=2, label='Docker — Ручной', color='#E57373')
ax.fill(angles, docker_manual_scores, alpha=0.1, color='#E57373')

ax.plot(angles, docker_auto_scores, 's-', linewidth=2, label='Docker — Авто', color='#81C784')
ax.fill(angles, docker_auto_scores, alpha=0.1, color='#81C784')

ax.plot(angles, nodocker_manual_scores, '^-', linewidth=2, label='Non-Docker — Ручной', color='#FFB74D')
ax.fill(angles, nodocker_manual_scores, alpha=0.1, color='#FFB74D')

ax.plot(angles, nodocker_auto_scores, 'd-', linewidth=2, label='Non-Docker — Авто', color='#64B5F6')
ax.fill(angles, nodocker_auto_scores, alpha=0.1, color='#64B5F6')

ax.set_xticks(angles[:-1])
ax.set_xticklabels(categories_radar, fontsize=12)
ax.set_ylim(0, 10)
ax.set_yticks([2, 4, 6, 8, 10])
ax.set_yticklabels(['2', '4', '6', '8', '10'], fontsize=9)
ax.legend(loc='upper right', bbox_to_anchor=(1.35, 1.1))
ax.set_title('Сравнение подходов по критериям (оценка 1-10)', fontsize=13, y=1.08)

plt.tight_layout()
plt.savefig('график_6_радар.png', dpi=150, bbox_inches='tight')
plt.show()

print("Все графики сохранены!")
print("Файлы:")
print("  - график_1_сравнение_времени_оба_типа.png")
print("  - график_2_средние_значения.png")
print("  - график_3_распределение_времени.png")
print("  - график_4_суммарные_затраты.png")
print("  - график_5_операции_и_отказы.png")
print("  - график_6_радар.png")
