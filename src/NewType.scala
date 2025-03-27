package tgtg

/** NewType is a type-safe way of creating a new type from an existing one. It is similar to a case class with a single
  * field, but without the runtime overhead.
  *
  * @example
  *
  * ```scala
  * object UserId extends NewType[String]
  * type UserId = UserId.Type
  * ```
  */
trait NewType[Wrapped]:
  opaque type Type <: Wrapped = Wrapped

  inline def apply(w: Wrapped): Type = w

  // Any typeclass that works for Wrapped also works for NewType
  inline given [F[_]](using F[Wrapped]): F[Type]                   = summon
  inline given [F[_, _]](using F[Wrapped, Wrapped]): F[Type, Type] = summon

end NewType
