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
package org.calypsonet.keyple.demo.reload.remote.ui

import android.content.Intent
import android.os.Bundle
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.databinding.ActivityPaymentValidatedBinding

class PaymentValidatedActivity : AbstractDemoActivity() {

  private lateinit var activityPaymentValidatedBinding: ActivityPaymentValidatedBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityPaymentValidatedBinding = ActivityPaymentValidatedBinding.inflate(layoutInflater)
    toolbarBinding = activityPaymentValidatedBinding.appBarLayout
    setContentView(activityPaymentValidatedBinding.root)
    activityPaymentValidatedBinding.chargeBtn.text = getString(R.string.load_card)
    activityPaymentValidatedBinding.chargeBtn.setOnClickListener {
      val intent = Intent(this, ReloadActivity::class.java)
      intent.putExtras(getIntent())
      startActivity(intent)
      this@PaymentValidatedActivity.finish()
    }
  }

  override fun onResume() {
    super.onResume()
    activityPaymentValidatedBinding.animation.setAnimation("tick_anim.json")
    activityPaymentValidatedBinding.animation.repeatCount = 0
    activityPaymentValidatedBinding.animation.playAnimation()
  }
}
