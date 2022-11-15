package utils.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {

    public static Jedis getConnect(){
        Jedis jedis = null;
        //Redis的配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(150);
        jedisPoolConfig.setMaxIdle(50);
        jedisPoolConfig.setMinIdle(30);
        jedisPoolConfig.setMaxWaitMillis(3 * 1000);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(500);
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(1000);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(1000);
        jedisPoolConfig.setNumTestsPerEvictionRun(100);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,"192.168.10.100",6379,5000);
        try {
            jedis = jedisPool.getResource();
            return jedis;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return new Jedis("192.168.10.100",6379);
        }
    }
}
