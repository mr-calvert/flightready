package flightready

/** Typeclass connecting a base type `B` to refined type `R` through a value level predicate.
  *
  * See https://en.wikipedia.org/wiki/Refinement_(computing)#Refinement_types
  * Ideally Refinement types are expressed using a tool like Typelevel's Refined project
  * https://github.com/fthomas/refined which captures the predicate at the type level.
  *
  * For the purposes of the Flightready project true type level expression of refinement is potentially overkill.
  * Adding a core typeclass dependency on Refined, which transitively depends on Shapeless is too heavy for the
  * conceptual correctness it brings.
  *
  * This typeclass exists as a lighter weight alternative, expressing the Base <-> Refined <-> predicate relationship
  * at value level while allowing for an underlying representation using Refined or competing libraries. Flightready
  * modules which implement concrete implementations of core Flightready algebras are thus free to reject or embrace
  * specific methods of refining critical types as they see fit.
  *
  * Does NOT assume any specific relationship between B and T. This provides maximum flexibility for B and T
  * implementations. It does require use of `refineV` and `widenV` to transport values between B and T, but it allows
  * for various forms of newtype simulation, subtyping, or totally unrelated types to be used.
  *
  * @tparam B base type restricted by `predicate` leading to `R`
  * @tparam T resulting refined type
  */
trait Refinement[B, T] {
  /** Defines the values allowed inside `R` */
  def predicate(b: B): Boolean

  def refine(b: B): Option[T]
  def widen(r: T): T
}
