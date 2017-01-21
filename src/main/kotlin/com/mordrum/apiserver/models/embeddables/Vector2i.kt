package com.mordrum.apiserver.models.embeddables

import javax.persistence.Embeddable

@Embeddable
class Vector2i(val x: Int = 0, val z: Int = 0) {
}
