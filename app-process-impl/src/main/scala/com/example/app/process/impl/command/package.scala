package com.example.app.process.impl

import com.example.app.process.impl.confirmation.Confirmation

package object command {

  type ReplyTo = akka.actor.typed.ActorRef[Confirmation]

}
