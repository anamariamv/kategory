package kategory

import io.kotlintest.KTestJUnitRunner
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class StateTTests : UnitSpec() {

    init {

        val instances = StateT.monadState<TryHK, Int>(Try.monad())

        testLaws(MonadStateLaws.laws(
                instances,
                object : Eq<StateTKind<TryHK, Int, Int>> {
                    override fun eqv(a: StateTKind<TryHK, Int, Int>, b: StateTKind<TryHK, Int, Int>): Boolean =
                            a.runM(1) == b.runM(1)

                },
                object : Eq<StateTKind<TryHK, Int, Unit>> {
                    override fun eqv(a: StateTKind<TryHK, Int, Unit>, b: StateTKind<TryHK, Int, Unit>): Boolean =
                            a.runM(1) == b.runM(1)
                }))

        testLaws(SemigroupKLaws.laws(
                StateT.semigroupK<ListKWHK, Int>(ListKW.monad(), ListKW.semigroupK()),
                StateT.applicative<ListKWHK, Int>(ListKW.monad()),
                object : Eq<HK<StateTKindPartial<ListKWHK, Int>, Int>> {
                    override fun eqv(a: HK<StateTKindPartial<ListKWHK, Int>, Int>, b: HK<StateTKindPartial<ListKWHK, Int>, Int>): Boolean =
                            a.runM(1) == b.runM(1)
                }))
        
        testLaws(MonadCombineLaws.laws(StateT.monadCombine<ListKWHK, Int>(ListKW.monadCombine()),
                { StateT.lift(ListKW.pure(it), ListKW.monad()) },
                { StateT.lift(ListKW.pure({ s: Int -> s * 2 }), ListKW.monad()) },
                object : Eq<HK<StateTKindPartial<ListKWHK, Int>, Int>> {
                    override fun eqv(a: HK<StateTKindPartial<ListKWHK, Int>, Int>, b: HK<StateTKindPartial<ListKWHK, Int>, Int>): Boolean =
                            a.runM(1) == b.runM(1)
                },
                object : Eq<HK<StateTKindPartial<ListKWHK, Int>, (Int) -> Int>> {
                    override fun eqv(a: HK<StateTKindPartial<ListKWHK, Int>, (Int) -> Int>, b: HK<StateTKindPartial<ListKWHK, Int>, (Int) -> Int>): Boolean =
                            a.runM(1) == b.runM(1)
                }))

    }
}
