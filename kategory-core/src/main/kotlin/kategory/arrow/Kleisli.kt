package kategory

typealias KleisliFun<F, D, A> = (D) -> HK<F, A>
typealias ReaderT<F, D, A> = Kleisli<F, D, A>

@higherkind
class Kleisli<F, D, A>(val MF: Monad<F>, val run: KleisliFun<F, D, A>) : KleisliKind<F, D, A> {

    fun <B> map(f: (A) -> B): Kleisli<F, D, B> = Kleisli(MF, { a -> MF.map(run(a), f) })

    fun <B> flatMap(f: (A) -> Kleisli<F, D, B>): Kleisli<F, D, B> =
            Kleisli(MF, { d ->
                MF.flatMap(run(d)) { a -> f(a).run(d) }
            })

    fun <B> zip(o: Kleisli<F, D, B>): Kleisli<F, D, Tuple2<A, B>> =
            flatMap({ a ->
                o.map({ b -> Tuple2(a, b) })
            })

    fun <DD> local(f: (DD) -> D): Kleisli<F, DD, A> = Kleisli(MF, { dd -> run(f(dd)) })

    infix fun <C> andThen(f: Kleisli<F, A, C>): Kleisli<F, D, C> = andThen(f.run)

    infix fun <B> andThen(f: (A) -> HK<F, B>): Kleisli<F, D, B> = Kleisli(MF, { MF.flatMap(run(it), f) })

    infix fun <B> andThen(a: HK<F, B>): Kleisli<F, D, B> = andThen({ a })

    companion object {

        inline operator fun <reified F, D, A> invoke(noinline run: KleisliFun<F, D, A>, MF: Monad<F> = monad<F>()): Kleisli<F, D, A> = Kleisli(MF, run)

        @JvmStatic inline fun <reified F, D, A> pure(x: A, MF: Monad<F> = monad<F>()): Kleisli<F, D, A> = Kleisli(MF, { _ -> MF.pure(x) })

        @JvmStatic inline fun <reified F, D> ask(MF: Monad<F> = monad<F>()): Kleisli<F, D, D> = Kleisli(MF, { MF.pure(it) })

        fun <F, D> instances(MF: Monad<F>): KleisliInstances<F, D> = object : KleisliInstances<F, D> {
            override fun MF(): Monad<F> = MF
        }

        inline fun <reified F, D> functor(MF: Monad<F> = monad<F>()): Functor<KleisliKindPartial<F, D>> = instances(MF)

        inline fun <reified F, D> applicative(MF: Monad<F> = monad<F>()): Applicative<KleisliKindPartial<F, D>> = instances(MF)

        inline fun <reified F, D> monad(MF: Monad<F> = monad<F>()): Monad<KleisliKindPartial<F, D>> = instances(MF)

        inline fun <reified F, D, reified E> monadError(MFE: MonadError<F, E> = monadError<F, E>()): MonadError<KleisliKindPartial<F, D>, E> =
                object : KleisliMonadError<F, D, E> {
                    override fun MF(): Monad<F> = MFE

                    override fun MFE(): MonadError<F, E> = MFE
                }
    }

}

inline fun <reified F, D, A> Kleisli<F, D, Kleisli<F, D, A>>.flatten(): Kleisli<F, D, A> = flatMap({ it })

inline fun <reified F, reified D, A> KleisliFun<F, D, A>.kleisli(FT: Monad<F> = monad()): Kleisli<F, D, A> = Kleisli(FT, this)
