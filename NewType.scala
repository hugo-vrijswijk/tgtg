package tgtg

/** NewType is a type that is a wrapper around another type, but has no runtime overhead.
  */
trait NewType[Wrapped]:
  opaque type Type <: Wrapped = Wrapped

  inline def apply(w: Wrapped): Type = w

  // Any typeclass that works for Wrapped also works for NewType
  given [F[_]](using F[Wrapped]): F[Type]                        = summon
  given (using CanEqual[Wrapped, Wrapped]): CanEqual[Type, Type] = summon

end NewType
