﻿// Copyright (c) 2025 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the
// terms of the BSD 3-Clause License which is available at
// https://opensource.org/licenses/BSD-3-Clause.
//
// SPDX-License-Identifier: BSD-3-Clause

using Newtonsoft.Json;

/// <summary>
/// MessageDto is a data transfer object for representing messages exchanged with the Keyple ticketing server.
/// </summary>
public class MessageDto
{
    /// <summary>
    /// API level.
    /// </summary>
    [JsonProperty("apiLevel")]
    public required int ApiLevel { get; set; }

    /// <summary>
    /// Session ID.
    /// </summary>
    [JsonProperty("sessionId")]
    public required string SessionId { get; set; }

    /// <summary>
    /// Action associated with the message.
    /// </summary>
    [JsonProperty("action")]
    public required string Action { get; set; }

    /// <summary>
    /// Client node ID.
    /// </summary>
    [JsonProperty("clientNodeId")]
    public required string ClientNodeId { get; set; }

    /// <summary>
    /// Server node ID.
    /// </summary>
    [JsonProperty("serverNodeId")]
    public string? ServerNodeId { get; set; }

    /// <summary>
    /// Name of the local reader.
    /// </summary>
    [JsonProperty("localReaderName")]
    public string? LocalReaderName { get; set; }

    /// <summary>
    /// Name of the remote reader.
    /// </summary>
    [JsonProperty("remoteReaderName")]
    public string? RemoteReaderName { get; set; }

    /// <summary>
    /// Body of the message.
    /// </summary>
    [JsonProperty("body")]
    public required string Body { get; set; }

    /// <summary>
    /// Sets the action associated with the message.
    /// </summary>
    /// <param name="action">The action to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetAction(string action)
    {
        this.Action = action;
        return this;
    }

    /// <summary>
    /// Sets the body of the message.
    /// </summary>
    /// <param name="body">The body to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetBody(string body)
    {
        this.Body = body;
        return this;
    }

    /// <summary>
    /// Sets the session ID.
    /// </summary>
    /// <param name="sessionId">The session ID to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetSessionId(string sessionId)
    {
        this.SessionId = sessionId;
        return this;
    }

    /// <summary>
    /// Sets the client node ID.
    /// </summary>
    /// <param name="clientNodeId">The client node ID to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetClientNodeId(string clientNodeId)
    {
        this.ClientNodeId = clientNodeId;
        return this;
    }

    /// <summary>
    /// Sets the name of the local reader.
    /// </summary>
    /// <param name="localReaderName">The name of the local reader to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetLocalReaderName(string localReaderName)
    {
        this.LocalReaderName = localReaderName;
        return this;
    }

    /// <summary>
    /// Sets the server node ID.
    /// </summary>
    /// <param name="serverNodeId">The server node ID to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetServerNodeId(string serverNodeId)
    {
        this.ServerNodeId = serverNodeId;
        return this;
    }

    /// <summary>
    /// Sets the name of the remote reader.
    /// </summary>
    /// <param name="remoteReaderName">The name of the remote reader to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetRemoteReaderName(string remoteReaderName)
    {
        this.RemoteReaderName = remoteReaderName;
        return this;
    }
}
