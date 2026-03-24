using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

// ── Database ──
var host     = Environment.GetEnvironmentVariable("DB_HOST")     ?? "localhost";
var port     = Environment.GetEnvironmentVariable("DB_PORT")     ?? "5432";
var dbName   = Environment.GetEnvironmentVariable("DB_NAME")     ?? "{{project_name}}_db";
var dbUser   = Environment.GetEnvironmentVariable("DB_USER")     ?? "postgres";
var dbPass   = Environment.GetEnvironmentVariable("DB_PASSWORD") ?? "postgres";
var connStr  = $"Host={host};Port={port};Database={dbName};Username={dbUser};Password={dbPass}";

builder.Services.AddDbContext<AppDbContext>(opt => opt.UseNpgsql(connStr));

// ── Controllers ──
builder.Services.AddControllers();

// ── CORS ──
builder.Services.AddCors(o => o.AddDefaultPolicy(p =>
    p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod()));

var app = builder.Build();

// Auto-create tables (dev convenience)
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    db.Database.EnsureCreated();
}

app.UseCors();
app.MapControllers();
app.Run();