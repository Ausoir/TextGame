package aoa.game

class Deck(var me: UnitModel, var moves: MutableList<MoveModel>, handSize: Int) {
    private val discard = arrayListOf<Int>()
    private var hand = IntArray(handSize)
    private var queue = arrayListOf<Int>()
    private var moves2 = mutableListOf<MoveModel>()

    private fun incHand(place: Int){
        discard.add(hand[place])
        if (queue.isNotEmpty()){
            hand[place] = queue.removeAt(0)
        } else {
            hand[place] = -1
        }
    }

    fun getCard(place: Int): MoveModel {
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
                draw(place)
            }
        }
    }

    fun draw(place: Int): MoveModel {
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

    fun addNextMove(move: MoveModel){
        moves.add(move)
        queue.add(0, moves.size - 1)
    }

    fun getQueues(): ArrayList<String>{
        val result = arrayListOf<String>()
        hand.forEach { if (it >= 0){ result.add(moves[it].name)} }
        queue.forEach { if (it >= 0){ result.add(moves[it].name)} }
        result.add("gather")
        return result
    }

    fun clearMoves(){
        queue.clear()
        discard.clear()
        moves.clear()
        for (i in hand.indices){
            hand[i] = i
        }
    }

    fun switchMoves(){
        val temp = moves2
        moves2 = moves
        moves = temp
    }
}