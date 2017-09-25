package cash.andrew.coffee

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

  /**
   * The expected extraction percentage.  The amount of coffee grounds that will be dissolved into the coffee
   */
  private val EXPECTED_EXTRACTION = BigDecimal(0.19)

  /**
   * The amount of coffee to water ratio we want to maintain.  For each unit of coffee, there should be 17.42 units
   * of water.
   */
  private val RATIO = BigDecimal(17.42)

  /**
   * Weight of one fluid ounce of water in grams
   */
  private val FLUID_OUNCE_TO_GRAM = BigDecimal(29.5735)

  /**
   * How much water should be used to bloom
   */
  private val BLOOM_PERCENTAGE = BigDecimal(0.1)

  private val WATER_ABSORBED_PERCENTAGE = BigDecimal(2)

  private val compositeDisposable = CompositeDisposable()

  @BindView(R.id.amount_of_coffee) lateinit var coffeeAmount : EditText
  @BindView(R.id.coffeee_output_text) lateinit var coffeeOutput : TextView
  @BindView(R.id.bloom_output_text) lateinit var bloomOutput : TextView
  @BindView(R.id.water_ouput_text) lateinit var waterOutput : TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)
  }

  override fun onResume() {
    super.onResume()

    val targetYield = RxTextView.afterTextChangeEvents(this.coffeeAmount)
        .debounce(100, TimeUnit.MILLISECONDS)
        .map { it.editable().toString() }
        .map {
          try {
            BigDecimal(it)
          } catch (e: NumberFormatException) {
            BigDecimal.ZERO
          }
        }
        .map { it * FLUID_OUNCE_TO_GRAM }
        .ioToMainScheduler
        .share()

    val coffeeAmount = targetYield.map { it / (RATIO + (-WATER_ABSORBED_PERCENTAGE + EXPECTED_EXTRACTION)) }
        .map { it.twoDecimals }
        .map { it.toPlainString() }

    val waterAmount = targetYield.map { RATIO * it / ((-WATER_ABSORBED_PERCENTAGE + EXPECTED_EXTRACTION) + RATIO) }
        .share()

    val bloomAmount = waterAmount.map { it * BLOOM_PERCENTAGE }
        .map { it.twoDecimals }
        .map { it.toPlainString() }

    compositeDisposable.add(waterAmount.map { it.twoDecimals }
        .map { it.toPlainString() }
        .subscribe { waterOutput.text = it })

    compositeDisposable.add(bloomAmount.subscribe { bloomOutput.text = it })
    compositeDisposable.add(coffeeAmount.subscribe { coffeeOutput.text = it })
  }

  override fun onPause() {
    compositeDisposable.clear()
    super.onPause()
  }
}
