using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// PostgreSQL
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? $"Host={Environment.GetEnvironmentVariable("DB_HOST") ?? "localhost"};" +
       $"Port={Environment.GetEnvironmentVariable("DB_PORT") ?? "5432"};" +
       $"Database={Environment.GetEnvironmentVariable("DB_NAME") ?? "{{project_name}}_db"};" +
       $"Username={Environment.GetEnvironmentVariable("DB_USER") ?? "postgres"};" +
       $"Password={Environment.GetEnvironmentVariable("DB_PASSWORD") ?? "postgres"}";

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(connectionString));

var app = builder.Build();

app.UseCors();
app.MapControllers();

app.Run();