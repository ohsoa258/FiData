﻿// Copyright (c) Microsoft. All rights reserved.

var builder = WebApplication.CreateBuilder(args);
// builder.WebHost.UseUrls("https://*:7089");

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddCors(c => c.AddPolicy("LimitRequests", policy =>
{
    policy.AllowAnyMethod()
          .SetIsOriginAllowed(_ => true)
          .AllowAnyHeader()
          .AllowCredentials();
}));

var app = builder.Build();

app.UseCors("LimitRequests");

// Configure the HTTP request pipeline.
app.UseSwagger();
app.UseSwaggerUI();

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
