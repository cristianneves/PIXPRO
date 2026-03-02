import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '~/components/ui/Button';
import { detectionService } from '~/services/detectionService';
// Adicionei ícones para os modelos (Google e Brain)
import { TbScanEye, TbLoader, TbArrowLeft, TbBrandGoogle, TbBrain } from 'react-icons/tb';
import styles from './styles.module.css';

export function ObjectDetectionPage() {
  const navigate = useNavigate();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [tags, setTags] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // NOVO: Estado para controlar qual IA será usada
  const [provider, setProvider] = useState<'gemini' | 'clarifai'>('gemini');

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      setSelectedFile(file);
      
      const objectUrl = URL.createObjectURL(file);
      setPreviewUrl(objectUrl);
      
      setTags([]);
      setError(null);
      
      // Passamos o arquivo para análise
      analyzeImage(file);
    }
  };

  const analyzeImage = async (file: File) => {
    setLoading(true);
    try {
      // ATUALIZADO: Passamos o 'provider' selecionado para o serviço
      const detectedTags = await detectionService.detectObjects(file, provider);
      setTags(detectedTags);
    } catch (err) {
      console.error('Erro na detecção:', err);
      setError('Falha ao analisar a imagem. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setSelectedFile(null);
    setPreviewUrl(null);
    setTags([]);
    setError(null);
  };

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <h1 className={styles.title}>Detecção de Objetos</h1>
        <Button variant="secondary" onClick={() => navigate('/app/home-selection')}>
          <TbArrowLeft /> Voltar
        </Button>
      </header>

      {/* Seletor de Modelo de IA */}
      {!selectedFile && (
        <div className={styles.modelSelector}>
          <button
            className={`${styles.modelButton} ${provider === 'gemini' ? styles.modelActive : ''}`}
            onClick={() => setProvider('gemini')}
          >
            <TbBrandGoogle size={20} />
            Google Gemini
          </button>

          <button
            className={`${styles.modelButton} ${provider === 'clarifai' ? `${styles.modelActive} ${styles.clarifai}` : ''}`}
            onClick={() => setProvider('clarifai')}
          >
            <TbBrain size={20} />
            Clarifai
          </button>
        </div>
      )}

      {!selectedFile ? (
        /* Estado Inicial: Upload */
        <label className={styles.uploadSection}>
          <TbScanEye size={48} style={{ marginBottom: '1rem' }} />
          <h2>Clique para analisar com {provider === 'gemini' ? 'Gemini' : 'Clarifai'}</h2>
          <p>Suportamos JPG e PNG. A IA identificará os objetos na cena.</p>
          <input 
            type="file" 
            accept="image/*" 
            onChange={handleFileSelect} 
            className={styles.hiddenInput} 
          />
        </label>
      ) : (
        /* Estado de Resultado */
        <div className={styles.contentGrid}>
          
          <div className={styles.imageContainer}>
            {previewUrl && <img src={previewUrl} alt="Preview" className={styles.previewImage} />}
          </div>

          <div className={styles.resultsContainer}>
            {loading ? (
              <div className={styles.loadingContainer}>
                <TbLoader size={30} className="spin" />
                {/* Mostra qual IA está trabalhando */}
                <p>Analisando imagem com {provider === 'gemini' ? 'Gemini' : 'Clarifai'}...</p>
              </div>
            ) : error ? (
              <div style={{ textAlign: 'center' }}>
                <p style={{ color: 'var(--destructive-fg)' }}>{error}</p>
                <Button onClick={() => selectedFile && analyzeImage(selectedFile)} variant="secondary">
                  Tentar Novamente
                </Button>
              </div>
            ) : (
              <>
                <h3 className={styles.resultsTitle}>
                  <TbScanEye /> Objetos Detectados ({tags.length})
                </h3>
                
                {/* Exibe qual modelo foi usado no resultado */}
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '-0.5rem', marginBottom: '1rem' }}>
                  Processado por: <strong>{provider === 'gemini' ? 'Google Gemini' : 'Clarifai'}</strong>
                </p>

                <div className={styles.tagsGrid}>
                  {tags.map((tag, index) => (
                    <span 
                      key={index} 
                      className={styles.tag}
                      style={{ animationDelay: `${index * 0.05}s` }}
                    >
                      {tag}
                    </span>
                  ))}
                </div>
                
                <div style={{ marginTop: '2rem', textAlign: 'right' }}>
                  <Button onClick={handleReset} variant="secondary">
                    Analisar Outra Imagem
                  </Button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}