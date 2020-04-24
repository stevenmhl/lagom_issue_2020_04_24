package com.example.app.process.impl

package object state {

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[com.example.app.process.impl.event.Event, State]

}
