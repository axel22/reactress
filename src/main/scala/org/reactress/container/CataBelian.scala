package org.reactress
package container



import scala.collection._



class CataBelian[@spec(Int, Long, Double) T, @spec(Int, Long, Double) S]
  (val get: S => T, val zero: T, val op: (T, T) => T, val inv: (T, T) => T)
  (implicit val canT: Arrayable[T], val canS: Arrayable[S])
extends ReactCatamorph[T, S] with ReactBuilder[S, CataBelian[T, S]] {
  import CataBelian._

  private[reactress] var elements: ReactTable[S, T] = null
  private var insertsEmitter: Reactive.Emitter[S] = null
  private var removesEmitter: Reactive.Emitter[S] = null
  private var value: ReactCell[T] = null

  def inserts: Reactive[S] = insertsEmitter

  def removes: Reactive[S] = removesEmitter

  def init(z: T) {
    elements = ReactTable[S, T]
    insertsEmitter = new Reactive.Emitter[S]
    removesEmitter = new Reactive.Emitter[S]
    value = ReactCell[T](zero)
  }

  init(zero)

  def signal = value

  def +=(v: S): Boolean = {
    if (!elements.contains(v)) {
      val x = get(v)
      elements(v) = x
      value := op(value(), x)
      insertsEmitter += v
      true
    } else false
  }

  def -=(v: S): Boolean = {
    if (elements.contains(v)) {
      val y = elements(v)
      elements.remove(v)
      value := inv(value(), y)
      removesEmitter += v
      true
    } else false
  }

  def container = this

  def push(v: S): Boolean = {
    if (elements.contains(v)) {
      val y = elements(v)
      val x = get(v)
      elements(v) = x
      value := op(inv(value(), y), x)
      true
    } else false
  }

  def size = elements.size

  def foreach(f: S => Unit) = elements.foreach((k, v) => f(k))
}


object CataBelian {

  def apply[@spec(Int, Long, Double) T](implicit g: Abelian[T], can: Arrayable[T]) = {
    new CataBelian[T, T](v => v, g.zero, g.operator, g.inverse)
  }

  implicit def factory[@spec(Int, Long, Double) T: Abelian: Arrayable] =
    new ReactBuilder.Factory[T, CataBelian[T, T]] {
      def apply() = CataBelian[T]
    }

}