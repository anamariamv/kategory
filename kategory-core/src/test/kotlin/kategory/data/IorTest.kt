package kategory

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import kategory.Ior.Right
import org.junit.runner.RunWith


@RunWith(KTestJUnitRunner::class)
class IorTest : UnitSpec() {

    init {

        val intIorMonad = Ior.monad(IntMonoid)

        testLaws(MonadLaws.laws(intIorMonad, Eq.any()))
        testLaws(TraverseLaws.laws(Ior.traverse(), Ior.applicative<Int>(), ::Right, Eq.any()))

        "bimap() should allow modify both value" {
            forAll { a: Int, b: String ->
                {
                    Ior.Right(b).bimap({ "5" }, { a * 2 }) == Ior.Right(a * 2) &&
                            Ior.Left(a).bimap({ a * 3 }, { "5" }) == Ior.Left(a * 3) &&
                            Ior.Both(a, b).bimap({ 2 }, { "power of $it" }) == Ior.Both(2, "power of $b")
                }()
            }
        }

        "mapLeft() should modify only left value" {
            forAll { a: Int, b: String ->
                {
                    Ior.Right(b).mapLeft { a * 2 } == Ior.Right(b) &&
                            Ior.Left(a).mapLeft { b } == Ior.Left(b) &&
                            Ior.Both(a, b).mapLeft { "power of $it" } == Ior.Both("power of $a", b)
                }()
            }
        }

        "swap() should interchange value" {
            forAll { a: Int, b: String ->
                {
                    Ior.Both(a, b).swap() == Ior.Both(b, a)
                }()
            }
        }

        "swap() should interchange entity" {
            forAll { a: Int ->
                {
                    Ior.Left(a).swap() == Ior.Right(a) &&
                            Ior.Right(a).swap() == Ior.Left(a)
                }()
            }
        }

        "unwrap() should return the isomorphic either" {
            forAll { a: Int, b: String ->
                {
                    Ior.Left(a).unwrap() == Either.Left(Either.Left(a)) &&
                            Ior.Right(b).unwrap() == Either.Left(Either.Right(b)) &&
                            Ior.Both(a, b).unwrap() == Either.Right(Pair(a, b))
                }()
            }
        }

        "pad() should return the correct Pair of Options" {
            forAll { a: Int, b: String ->
                {
                    Ior.Left(a).pad() == Pair(Option.Some(a), Option.None) &&
                            Ior.Right(b).pad() == Pair(Option.None, Option.Some(b)) &&
                            Ior.Both(a, b).pad() == Pair(Option.Some(a), Option.Some(b))
                }()
            }
        }

        "toEither() should convert values into a valid Either" {
            forAll { a: Int, b: String ->
                {
                    Ior.Left(a).toEither() == Either.Left(a) &&
                            Ior.Right(b).toEither() == Either.Right(b) &&
                            Ior.Both(a, b).toEither() == Either.Right(b)
                }()
            }
        }

        "toOption() should convert values into a valid Option" {
            forAll { a: Int, b: String ->
                {
                    Ior.Left(a).toOption() == Option.None &&
                            Ior.Right(b).toOption() == Option.Some(b) &&
                            Ior.Both(a, b).toOption() == Option.Some(b)
                }()
            }
        }

        "fromOption() should build a correct Option<Ior>" {
            forAll { a: Int, b: String ->
                {
                    Ior.fromOptions(Option.Some(a), Option.None) == Option.Some(Ior.Left(a)) &&
                            Ior.fromOptions(Option.Some(a), Option.Some(b)) == Option.Some(Ior.Both(a, b)) &&
                            Ior.fromOptions(Option.None, Option.Some(b)) == Option.Some(Ior.Right(b)) &&
                            Ior.fromOptions(Option.None, Option.None) == Option.None
                }()
            }
        }


        "getOrElse() should return value" {
            forAll { a: Int, b: Int ->
                Ior.Right(a).getOrElse { b } == a &&
                        Ior.Left(a).getOrElse { b } == b &&
                        Ior.Both(a, b).getOrElse { a * 2 } == b
            }

        }

        "Ior.monad.flatMap should combine left values" {
            val ior1 = Ior.Both(3, "Hello, world!")
            val iorResult = intIorMonad.flatMap(ior1, { Ior.Left(7) })
            iorResult shouldBe Ior.Left(10)
        }

    }
}