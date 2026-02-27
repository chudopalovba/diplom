using Microsoft.AspNetCore.Mvc;

namespace {{project_name}}.Controllers;

[ApiController]
[Route("api")]
public class HelloController : ControllerBase
{
    [HttpGet("health")]
    public IActionResult Health()
    {
        return Ok(new { status = "UP", project = "{{PROJECT_NAME}}" });
    }

    [HttpGet("hello")]
    public IActionResult Hello()
    {
        return Ok(new { message = "Hello from {{PROJECT_NAME}}!" });
    }
}