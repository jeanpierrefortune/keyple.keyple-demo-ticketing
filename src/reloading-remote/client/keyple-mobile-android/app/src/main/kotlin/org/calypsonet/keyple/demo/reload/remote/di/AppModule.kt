/* ******************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the BSD 3-Clause License which is available at
 * https://opensource.org/licenses/BSD-3-Clause.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 ****************************************************************************** */
package org.calypsonet.keyple.demo.reload.remote.di

import android.content.Context
import dagger.Binds
import dagger.Module
import org.calypsonet.keyple.demo.reload.remote.Application

@Suppress("unused")
@Module
abstract class AppModule {
  // expose Application as an injectable context
  @Binds abstract fun bindContext(application: Application?): Context?
}
