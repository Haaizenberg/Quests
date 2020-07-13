package com.example.quests

import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.event_member_row_item.view.*

class MemberItem(private val simpleUser: SimpleUser): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_member_username.text = simpleUser.username
        Picasso
            .get()
            .load(simpleUser.profileImageUrl)
            .into(viewHolder.itemView.imageview_member)
    }
    override fun getLayout(): Int {
        return R.layout.event_member_row_item
    }
}