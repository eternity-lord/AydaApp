package com.aydanailroom.app.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aydanailroom.app.R

@StringRes
fun expenseCategoryLabelId(key: String): Int = when (key) {
    "gel" -> R.string.expense_cat_gel
    "polish" -> R.string.expense_cat_polish
    "files" -> R.string.expense_cat_files
    "tools" -> R.string.expense_cat_tools
    "rent" -> R.string.expense_cat_rent
    "transport" -> R.string.expense_cat_transport
    "ads" -> R.string.expense_cat_ads
    "other" -> R.string.expense_cat_other
    "income_service" -> R.string.finance_income_category
    else -> R.string.expense_cat_other
}

@Composable
fun expenseCategoryName(key: String): String = stringResource(expenseCategoryLabelId(key))
