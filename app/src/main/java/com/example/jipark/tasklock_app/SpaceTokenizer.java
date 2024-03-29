package com.example.jipark.tasklock_app;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

/**
 * Created by jipark on 10/6/17.
 */

public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {

    @Override
    public int findTokenStart(CharSequence charSequence, int cursor) {
        int i = cursor;

        while (i > 0 && !Character.isWhitespace(charSequence.charAt(i - 1))) {
            i--;
        }
        while (i < cursor && Character.isWhitespace(charSequence.charAt(i))) {
            i++;
        }

        return i;
    }

    @Override
    public int findTokenEnd(CharSequence charSequence, int cursor) {
        int i = cursor;
        int len = charSequence.length();

        while (i < len) {
            if (Character.isWhitespace(charSequence.charAt(i))) {
                return i;
            } else {
                i++;
            }
        }

        return len;
    }

    @Override
    public CharSequence terminateToken(CharSequence charSequence) {
        int i = charSequence.length();

        while (i > 0 && Character.isWhitespace(charSequence.charAt(i - 1))) {
            i--;
        }

        if (i > 0 && Character.isWhitespace(charSequence.charAt(i - 1))) {
            return charSequence;
        } else {
            if (charSequence instanceof Spanned) {
                SpannableString sp = new SpannableString(charSequence + " ");
                TextUtils.copySpansFrom((Spanned)charSequence, 0, charSequence.length(),
                        Object.class, sp, 0);
                return sp;
            } else {
                return charSequence + " ";
            }
        }
    }
}

