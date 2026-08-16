package com.remine.family.adapter.infrastructure.jpa

import java.util.UUID

interface PostReplyCountProjection {
    fun getPostId(): UUID
    fun getCount(): Long
}
