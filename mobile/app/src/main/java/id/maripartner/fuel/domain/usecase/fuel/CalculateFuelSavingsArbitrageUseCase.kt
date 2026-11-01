package id.maripartner.fuel.domain.usecase.fuel

import id.maripartner.fuel.domain.model.FuelBrandPrice
import id.maripartner.fuel.domain.model.FuelSavingsArbitrageResult
import kotlin.math.abs

class CalculateFuelSavingsArbitrageUseCase {

    operator fun invoke(
        brandA: FuelBrandPrice,
        brandB: FuelBrandPrice,
        tankCapacityLiters: Double
    ): FuelSavingsArbitrageResult {
        val priceDiff = abs(brandA.pricePerLiterIdr - brandB.pricePerLiterIdr)
        val savingsPerTank = (priceDiff.toDouble() * tankCapacityLiters).toLong()
        val monthlySavings = savingsPerTank * 4L // Asumsi 4x isi tangki sebulan

        val cheaperBrand = if (brandA.pricePerLiterIdr < brandB.pricePerLiterIdr) {
            brandA.brand
        } else if (brandB.pricePerLiterIdr < brandA.pricePerLiterIdr) {
            brandB.brand
        } else {
            "Sama Hemat"
        }

        return FuelSavingsArbitrageResult(
            gradeCategory = brandA.gradeCategory,
            tankCapacityLiters = tankCapacityLiters,
            brandA = brandA,
            brandB = brandB,
            priceDiffPerLiter = priceDiff,
            savingsPerFullTankIdr = savingsPerTank,
            estimatedMonthlySavingsIdr = monthlySavings,
            cheaperBrandName = cheaperBrand
        )
    }
}
