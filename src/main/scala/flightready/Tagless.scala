package flightready

// TODO: Consider pulling this into a note and ditching Run
// TODO: Review that it makes sense without C and MkIO
// TODO: What to say about Modules
/** Types core to our particular Finally Tagless encoding.
  *
  * Our tagless encoding is built five key families of types: IO, F, and Run.
  *  * `IO` --
  *    * Traits define abstract interfaces for classes of services, often called the algebra of a service. A service
  *    algebra's operations are defined on some implicit resource. A `FileIO` trait would describe operations possible
  *    on a closed over file. Trait methods return lazy operations within an `F` wrapper. Business logic implemented on
  *    service algebras in IO traits allow for great flexibility as the F wrapper type and concrete IO trait
  *    implementation are late bound.
  *
  *    Flightready uses finely vertically decomposed algebras. A `FileIO` algebra would contain operations possible on
  *    a closed file (for instance size, delete, and touch). Operations on an open file would be part of a
  *    'FileHandleIO' algebra. `FileHandleIO` programs would be embedded in `FileIO` programs via a 'onOpenFile' or
  *    similar `FileIO` operation which opens the implied file, runs the program, then closes the file. This embedding
  *    ensures operations which commit scarce or leakable resources are handled safely.
  *
  *    * Classes --
  *    Concrete implementations of service algebras are provided by classes which implement one or more IO traits.
  *    Evaluating a function defined on an IO trait requires a concrete instance, aka an interpreter, to be injected
  *    into the business logic function. The choice of implementing class provides a context into which the function's
  *    specific business logic is translated. If the logic is being evaluated for its effects an interpreter which wraps
  *    interaction with a live service into an effect Monad for F (for example [[cats.effect.IO]]) would be chosen.
  *
  *    Alternatively the business logic can be translated into a Free structure, providing a symbolic reification of
  *    the logic function's intended effects. The flexibility to go directly to an effect Monad, or to an arbitrary
  *    symbolic reification motivates Flightready's choice to natively support a Finally Tagless encoding.
  *
  *  * 'F' --
  *  The `F` type denotes the wrapper in which effects are encoded. Universally Flightready makes no assumption about
  *  the `F` type other than its shape (a single parameter type constructor taking a concrete type). IO trait algebras
  *  are encouraged to apply the principle of least power and not further restrict the F into which they can be executed
  *  unless semantically required. Concrete IO interpreters are similarly encouraged to ask nothing more than they need,
  *  however these instances require a mechanism for closing over real effects or lifting symbols into an F.
  *
  *  Effectful interpreters may choose to preserve F parameterization by requiring a [[cats.effect.Sync]] on F. The
  *  `delay` operation lifts arbitrary impure effects into any F with a [[cats.effect.Sync]]
  *
  *  * `Run` --
  *  Type covering all business logic functions (aka programs) written on IO traits. `Run` functions take IO instances
  *  and embed their logic into F instances. `Run` functions will typically require Monad or Applicative instances for
  *  their F parameter and use those type classes' combinators to encode their structure.
  *
  */
object Tagless {
  /** Encodes a program built on type `IO`'s primitive operations into an `F`. */
  type Run[F[_], X, IO[_[_]]] = IO[F] => F[X]
}
