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

namespace App.domain.api
{
    /// <summary>
    /// Defines the operations that the main service API must support. 
    /// These operations primarily involve interacting with a card, where the card transaction
    /// is driven by the server provided to the implementation of this interface.
    /// </summary>
    public interface MainServiceApi
    {

        /// <summary>
        /// Blocks until a card is inserted. 
        /// </summary>
        void WaitForCardInsertion();

        /// <summary>
        /// Selects the card and reads contracts.
        /// The transaction is fully operated by the server.
        /// </summary>
        /// <returns>A string representation of the contracts read from the card.</returns>
        string SelectCardAndReadContracts();

        /// <summary>
        /// Selects the card and increases the counter of its MULTI_TRIP contract if it exists.
        /// The transaction is fully operated by the server.
        /// </summary>
        /// <param name="counterIncrement">The amount by which to increase the contract's counter.</param>
        /// <returns>A string representation of the status of the operation.</returns>
        string SelectCardAndIncreaseContractCounter(int counterIncrement);
    }
}
