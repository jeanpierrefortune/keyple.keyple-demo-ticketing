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

namespace App.domain.data.executeremoteservice
{
    /// <summary>
    /// Input data used for the write step.
    /// Currently empty.
    /// </summary>
    class InputDataWrite : InputData
    {
        [JsonProperty("counterIncrement")]
        public required string CounterIncrement { get; set; }
    }
}
