package cash.andrew.coffee

import java.math.BigDecimal


val BigDecimal.twoDecimals : BigDecimal get() = this.setScale(2, java.math.RoundingMode.HALF_UP)