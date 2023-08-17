// Copyright (c) Microsoft. All rights reserved.

using System.Text.Json.Serialization;

namespace semantic_kernel.KernelExtensions;

public class KernelSettings
{
    public const string DefaultConfigFile = "gpt.json";
    public const string OpenAI = "OPENAI";
    public const string AzureOpenAI = "AZUREOPENAI";
    [JsonPropertyName("serviceType")]
    public string ServiceType { get; set; } = string.Empty;
    [JsonPropertyName("serviceId")]
    public string ServiceId { get; set; } = string.Empty;
    [JsonPropertyName("deploymentOrModelId")]
    public string DeploymentOrModelId { get; set; } = string.Empty;
    [JsonPropertyName("endpoint")]
    public string Endpoint { get; set; } = string.Empty;
    [JsonPropertyName("apiKey")]
    public string ApiKey { get; set; } = string.Empty;
    [JsonPropertyName("orgId")]
    public string OrgId { get; set; } = string.Empty;

    public static KernelSettings LoadGptSettings()
    {
        try
        {
            if (File.Exists(DefaultConfigFile))
            {
                return SkFromFile(DefaultConfigFile);
            }
            throw new FileNotFoundException($"SK配置文件'{DefaultConfigFile}'不存在");
        }
        catch (InvalidDataException ide)
        {
            throw new InvalidDataException($"加载SK配置文件出错:{ide.Message}");
        }
    }

    private static KernelSettings SkFromFile(string configFile = DefaultConfigFile)
    {
        if (!File.Exists(configFile))
        {
            throw new FileNotFoundException($"配置文件不存在:{configFile}");
        }
        var configuration = new ConfigurationBuilder()
            .SetBasePath(System.IO.Directory.GetCurrentDirectory())
            .AddJsonFile(configFile,optional:true,reloadOnChange: true)
            .Build();
        return configuration.Get<KernelSettings>()
        ?? throw new InvalidDataException($"配置文件'{configFile}'加载错误，请检测配置文件内容是否有误");
    }
}
