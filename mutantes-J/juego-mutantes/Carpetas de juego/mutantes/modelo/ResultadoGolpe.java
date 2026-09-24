package mutantes.modelo;

/**
 * Resultado de recibir un golpe.
 *
 * @param dano       energía perdida
 * @param eliminado  true solo para el golpe que lleva la energía a 0
 * @param bloqueado  true si el objetivo estaba defendiendo
 */
public record ResultadoGolpe(int dano, boolean eliminado, boolean bloqueado) {}
