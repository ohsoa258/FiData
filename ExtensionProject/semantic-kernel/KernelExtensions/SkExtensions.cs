// Copyright (c) Microsoft. All rights reserved.

using System.Text.Json.Serialization;
using Microsoft.SemanticKernel;

namespace semantic_kernel.KernelExtensions;
public class SkExtensions
{
    internal static IKernel AddCompletionBackend(KernelSettings kernelSettings)
    {
        var builder = new KernelBuilder();
        switch (kernelSettings.ServiceType.ToUpperInvariant())
        {
            case KernelSettings.AzureOpenAI:
                builder.WithAzureChatCompletionService(
                    deploymentName: kernelSettings.DeploymentOrModelId,
                    endpoint: kernelSettings.Endpoint,
                    apiKey: kernelSettings.ApiKey);
                break;
            case KernelSettings.OpenAI:
                builder.WithOpenAIChatCompletionService(
                    modelId: kernelSettings.DeploymentOrModelId,
                    apiKey: kernelSettings.ApiKey);
                break;
        }
        var kernel = builder.Build();
        return kernel;
    }
}
