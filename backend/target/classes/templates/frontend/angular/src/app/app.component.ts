import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="app">
      <h1>{{PROJECT_NAME}}</h1>
      <p>{{ '{{' }} message {{ '}}' }}</p>
    </div>
  `,
  styles: [`
    .app {
      text-align: center;
      margin-top: 50px;
    }
  `]
})
export class AppComponent implements OnInit {
  message = 'Loading...';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<{ message: string }>('/api/hello').subscribe({
      next: (data) => this.message = data.message,
      error: () => this.message = '{{PROJECT_NAME}} — Frontend is running!'
    });
  }
}