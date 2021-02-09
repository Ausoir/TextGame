package com.example.textgame.game

class Deck(var me: UnitModel, val moves: ArrayList<MoveModel>, handSize: Int) {
    private val discard = arrayListOf<Int>()
    private var hand = IntArray(handSize)
    private var queue = arrayListOf<Int>()

    fun incHand(place: Int){
        discard.add(hand[place])
        if (queue.isNotEmpty()){
            hand[place] = queue.removeAt(0)
        } else {
            hand[place] = -1
        }
    }

    fun getCard(place: Int): MoveModel{
        return when {
            hand[place] == -1 -> {
                MoveModel.getWait("")
            }
            hand[place] == -2 -> {
                MoveModel.shuffle
            }
            me.isReqMet(moves[hand[place]].reqs).isEmpty() -> {
                moves[hand[place]]
            }
            else -> {
                incHand(place)
                getCard(place)
            }
        }
    }

    fun draw(place: Int): MoveModel{
        incHand(place)
        return getCard(place)
    }

    fun shuffle(){
        queue.clear()
        discard.clear()
        for (i in moves.indices){
            queue.add(i)
        }
        queue.shuffle()
        queue.add(-2)
        for (i in hand.indices){
            hand[i] = queue.removeAt(0)
        }
    }

    fun validate(){
        for (i in hand.indices){
            getCard(i)
        }
    }
}