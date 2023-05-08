package kr.hqservice.framework.database.component

import org.jetbrains.exposed.sql.SizedIterable

interface HQCrudRepository<T, ID> : HQRepository<T, ID> {
    /**
     * 주어진 entity 를 저장합니다.
     * 저장 후 entity instance 가 변경될 수 있으므로, 반환합니다.
     *
     * @return 저장된 entity
     */
    suspend fun <S : T> save(entity: S): S

    /**
     * 주어진 모든 entity 를 저장합니다.
     * 저장 후 entity instance 가 변경될 수 있으므로, 반환합니다.
     *
     * @return 저장된 entity
     */
    suspend fun <S : T> saveAll(entities: Iterable<S>): S

    /**
     * 주어진 id 를 통해 entity 를 찾습니다.
     *
     * @param id entity 의 id
     * @return 찾은 entity, 찾지 못한 경우 null 을 반환함.
     */
    suspend fun findById(id: ID): T?

    /**
     * 주어진 id 를 통해 entity 가 존재하는지 판별합니다.
     *
     * @param id entity 의 id
     * @return 존재하는지 여부, 존재할 경우 true 를 반환함.
     */
    suspend fun existsById(id: ID): Boolean

    /**
     * 전체 entity 를 반환합니다.
     *
     * @return 전체 entity
     */
    suspend fun findAll(): SizedIterable<T>

    /**
     * 주어진 id 들을 통해 모든 entity 를 찾습니다.
     *
     * @param ids entity 의 id 들
     * @return 찾은 entity 들
     */
    suspend fun findAllById(ids: Iterable<ID>): SizedIterable<T>

    /**
     * entity 들의 전체 수를 리턴합니다.
     *
     * @return 전체 entity 수
     */
    suspend fun count(): Long

    /**
     * 주어진 id 를 통해 entity 를 삭제합니다.
     *
     * @param id 삭제할 id
     */
    suspend fun deleteById(id: ID)

    /**
     * entity 를 삭제합니다.
     *
     * @param entity 삭제할 entity
     */
    suspend fun delete(entity: T)

    /**
     * 주어진 id 들을 통해 entity 를 삭제합니다.
     *
     * @param ids 삭제할 entity id 들
     */
    suspend fun deleteAllById(ids: Iterable<ID>)

    /**
     * entity 들을 삭제합니다.
     *
     * @param entities 삭제할 entity 들
     */
    suspend fun deleteAll(entities: Iterable<T>)
}