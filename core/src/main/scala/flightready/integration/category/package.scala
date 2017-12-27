package flightready.integration

/** Abstracting over Scalaz and Cats.
  *
  * Yes I'm going to try to abstract over both libraries, supporting
  * each as equal peers without depending on either in the core
  * library. I know it's like starting a land war in Asia or going
  * up against a Sicilian when Death is on the line, but like our
  * beloved hero I have a secret weapon.
  *
  * I'm not USING either Cats or Scalaz in the core library. I'm just
  * publishing instances of various typeclasses. Not the interesting
  * ones either, but the common, settled, core ones.
  *
  * The plan: establish library local typeclasses with just basis
  * operations from the typeclasses for which FlightReady will supply
  * instances. `Monad` is just `flatMap` and `Pure`, no `whileM` or
  * syntax. Plug in Cats and Scalaz integration libraries will
  * provide instance conversions to the full weight, batteries
  * included versions that come with the appropriate libraries.
  */
package object category {
  // This is just a sketch at the moment.

  // The doc comment is mostly to remind ME of my lunatic scheme in
  // case painkillers, family, or $job delay my work and I forget the
  // plan.

  // This package object is just here to provide a doc comment for
  // the module and to keep git/intellij from removing the empty
  // directory holding place for the abstractions.
}
