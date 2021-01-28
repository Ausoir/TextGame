package com.example.textgame.game

class Deck(var me: UnitModel, val moves: ArrayList<MoveModel>, handSize: Int) {
    private val discard = arrayListOf<Int>()
    private var hand = IntArray(handSize)

    fun incHand(place: Int){
        discard.add(hand[place])
        var resultIndex = hand[place] + 1
        while (resultIndex in hand || resultIndex in discard){
            resultIndex += 1
        }
        hand[place] = resultIndex
    }

    fun getCard(place: Int): MoveModel{
        return when {
            hand[place] > moves.size -> {
                MoveModel.getWait("")
            }
            hand[place] == moves.size -> {
                MoveModel.shuffle
            }
            me.isReqMet(moves[hand[place]].req).isEmpty() -> {
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
        discard.clear()
        moves.shuffle()
        for (i in hand.indices){
            hand[i] = i
        }
    }

    fun validate(){
        for (i in hand.indices){
            getCard(i)
        }
    }
}