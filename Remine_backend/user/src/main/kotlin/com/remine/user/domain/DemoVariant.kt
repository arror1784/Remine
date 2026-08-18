package com.remine.user.domain

/**
 * Which fixed seed-account pair a demo-login should log into. EVAL is the original,
 * unchanged pair (V8__seed_demo_users.sql) used for AI product review — its data must
 * never be reset or touched by anything demo-related. DEMO is a second pair
 * (V13__seed_demo_variant_users.sql) meant for live demos, whose data can be wiped and
 * reseeded on demand via the demo-reset endpoint without affecting EVAL.
 */
enum class DemoVariant {
    EVAL,
    DEMO,
}
