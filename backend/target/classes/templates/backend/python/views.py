from django.http import JsonResponse


def health(request):
    return JsonResponse({'status': 'UP', 'project': '{{PROJECT_NAME}}'})


def hello(request):
    return JsonResponse({'message': 'Hello from {{PROJECT_NAME}}!'})