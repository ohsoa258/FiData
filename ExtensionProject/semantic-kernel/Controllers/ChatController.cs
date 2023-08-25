// Copyright (c) Microsoft. All rights reserved.

using Microsoft.AspNetCore.Mvc;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.Orchestration;
using Microsoft.SemanticKernel.SkillDefinition;
using semantic_kernel.KernelExtensions;

namespace semantic_kernel.Controllers;

[Route("api/[controller]/[action]")]
[ApiController]
public class ChatController : ControllerBase
{
    public static KernelSettings kernelSettings = KernelSettings.LoadGptSettings();

    public static IKernel? myKernel = null;

    public static IDictionary<string, ISKFunction>? mySkill = null;
    [HttpPost]
    public IActionResult BuildChat()
    {
        myKernel = SkExtensions.AddCompletionBackend(kernelSettings);

        mySkill = myKernel.ImportSemanticSkillFromDirectory("Skills", "DM");

        return this.Ok("OK");
    }
    /// <summary>
    ///访问gpt
    /// </summary>
    /// <param name="prompt">输入内容</param>
    /// <returns></returns>
    [HttpPost]
    public async Task<IActionResult> ChatAsync([FromBody] Prompt prompt)
    {
        var myContext = new ContextVariables();
        myContext.Set("role", prompt.role);
        Console.WriteLine(prompt.role);
        myContext.Set("history", prompt.history);
        Console.WriteLine(prompt.history);
        myContext.Set("input", prompt.input);
        Console.WriteLine(prompt.input);
        var myResult = await myKernel.RunAsync(myContext, mySkill["GPT"]);
        Console.WriteLine(myResult.Result);
        return this.Ok(myResult.Result);
    }

    public class Prompt
    {
        public string role { get; set; } = string.Empty;
        public string history { get; set; } = string.Empty;
        public string input { get; set; } = string.Empty;
    }
}
