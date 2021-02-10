/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.qs;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.keyguard.CarrierText;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.Utils;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.DataUsageView;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;

public class OPQSFooter extends LinearLayout implements OnUserInfoChangedListener {

    protected View mEdit;
    protected TouchAnimator mFooterAnimator;

    private SettingsButton mSettingsButton;
    private ActivityStarter mActivityStarter;
    private FrameLayout mFooterActions;
    private DataUsageView mDataUsageView;
    private CarrierText mCarrierText;
    private MultiUserSwitch mMultiUserSwitch;
    private ImageView mMultiUserAvatar;

    public OPQSFooter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEdit = findViewById(R.id.edit);
        mSettingsButton = findViewById(R.id.settings_button);
        mFooterActions = findViewById(R.id.op_qs_footer_actions);
        mCarrierText = findViewById(R.id.qs_carrier_text);
        mDataUsageView = findViewById(R.id.data_usage_view);
        mDataUsageView.setVisibility(View.GONE);
        mMultiUserSwitch = findViewById(R.id.multi_user_switch);
        mMultiUserSwitch.setVisibility(View.GONE);
        mMultiUserSwitch.setClickable(mMultiUserSwitch.getVisibility() == View.VISIBLE);
        mMultiUserAvatar = mMultiUserSwitch.findViewById(R.id.multi_user_avatar);
        mFooterAnimator = createFooterAnimator();
    }

    public void setExpansion(float headerExpansionFraction) {
        if (mFooterAnimator != null) {
            mFooterAnimator.setPosition(headerExpansionFraction);
        }
    }

    public void setExpanded(boolean expanded) {
        if (mCarrierText != null && mDataUsageView != null) {
            mCarrierText.setVisibility(expanded ? View.GONE : View.VISIBLE);
            mDataUsageView.setVisibility(expanded ? View.VISIBLE : View.GONE);
            if (expanded) {
                mDataUsageView.updateUsage();
            }
        }
        if (mEdit != null && mMultiUserSwitch != null) {
            mEdit.setVisibility(expanded ? View.VISIBLE : View.GONE);
            mMultiUserSwitch.setVisibility(expanded &&
                   mMultiUserSwitch.isMultiUserEnabled() ? View.VISIBLE : View.GONE);
            mMultiUserSwitch.setClickable(mMultiUserSwitch.getVisibility() == View.VISIBLE);
        }
    }

    @Nullable
    private TouchAnimator createFooterAnimator() {
        return new TouchAnimator.Builder()
                .addFloat(mEdit, "alpha", 0, 1)
                .addFloat(mMultiUserSwitch, "alpha", 0, 1)
                .addFloat(mDataUsageView, "alpha", 0, 1)
                .setStartDelay(0.9f)
                .build();
    }

    public View getSettingsButton() {
        return mSettingsButton;
    }

    public View getEditButton() {
        return mEdit;
    }

    public void setOrientation(boolean isLandscape) {
        mFooterActions.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onUserInfoChanged(String name, Drawable picture, String userAccount) {
        if (picture != null &&
                UserManager.get(mContext).isGuestUser(KeyguardUpdateMonitor.getCurrentUser()) &&
                !(picture instanceof UserIconDrawable)) {
            picture = picture.getConstantState().newDrawable(mContext.getResources()).mutate();
            picture.setColorFilter(
                    Utils.getColorAttrDefaultColor(mContext, android.R.attr.colorForeground),
                    Mode.SRC_IN);
        }
        mMultiUserAvatar.setImageDrawable(picture);
    }
}
