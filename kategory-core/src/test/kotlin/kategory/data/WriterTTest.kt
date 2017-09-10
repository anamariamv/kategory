package kategory

import io.kotlintest.KTestJUnitRunner
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class WriterTTest : UnitSpec() {
    init {
        testLaws(MonadLaws.laws(WriterT.monad(NonEmptyList.monad(), IntMonoid), Eq.any()))
        testLaws(MonoidKLaws.laws(
                WriterT.monoidK<ListKWHK, Int>(ListKW.monad(), ListKW.monoidK()),
                WriterT.applicative(ListKW.monad(), IntMonoid),
                object : Eq<WriterTKind<ListKWHK, Int, Int>> {
                    override fun eqv(a: WriterTKind<ListKWHK, Int, Int>, b: WriterTKind<ListKWHK, Int, Int>): Boolean =
                            a.ev().value == b.ev().value
                }))

        testLaws(MonadWriterLaws.laws(WriterT.monad(Option.monad(), IntMonoid),
                WriterT.monadWriter(Option.monad(), IntMonoid),
                IntMonoid,
                genIntSmall(),
                genTuple(genIntSmall(), genIntSmall()),
                object : Eq<HK<WriterTKindPartial<OptionHK, Int>, Int>> {
                    override fun eqv(a: HK<WriterTKindPartial<OptionHK, Int>, Int>, b: HK<WriterTKindPartial<OptionHK, Int>, Int>): Boolean =
                            a.ev().value.ev().let { optionA: Option<Tuple2<Int, Int>> ->
                                val optionB = a.ev().value.ev()
                                optionA.fold({ optionB.fold({ true }, { false }) }, { value: Tuple2<Int, Int> -> optionB.fold({ false }, { value == it }) })
                            }
                },
                object : Eq<HK<WriterTKindPartial<OptionHK, Int>, Tuple2<Int, Int>>> {
                    override fun eqv(a: HK<WriterTKindPartial<OptionHK, Int>, Tuple2<Int, Int>>, b: HK<WriterTKindPartial<OptionHK, Int>, Tuple2<Int, Int>>): Boolean =
                            a.ev().value.ev().let { optionA: Option<Tuple2<Int, Tuple2<Int, Int>>> ->
                                val optionB = a.ev().value.ev()
                                optionA.fold({ optionB.fold({ true }, { false }) }, { value: Tuple2<Int, Tuple2<Int, Int>> -> optionB.fold({ false }, { value == it }) })
                            }
                }
        ))

        testLaws(MonadFilterLaws.laws(WriterT.monadFilter(Option.monadFilter(), IntMonoid),
                { WriterT(Option.monad(), Option(Tuple2(it, it))) },
                object : Eq<HK<WriterTKindPartial<OptionHK, Int>, Int>> {
                    override fun eqv(a: HK<WriterTKindPartial<OptionHK, Int>, Int>, b: HK<WriterTKindPartial<OptionHK, Int>, Int>): Boolean =
                            a.ev().value.ev().let { optionA: Option<Tuple2<Int, Int>> ->
                                val optionB = a.ev().value.ev()
                                optionA.fold({ optionB.fold({ true }, { false }) }, { value: Tuple2<Int, Int> -> optionB.fold({ false }, { value == it }) })
                            }
                }))
    }
}
