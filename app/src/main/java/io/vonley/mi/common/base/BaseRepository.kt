package io.vonley.mi.common.base

abstract class BaseRepository<T>(val dao: T) : Repository<T> {
}