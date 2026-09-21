import os
import logging
from typing import Dict, Any, Optional

import firebase_admin
from firebase_admin import credentials, auth

logger = logging.getLogger(__name__)

def initialize_firebase() -> None:
    """Initialize the Firebase Admin SDK if not already initialized."""
    if not firebase_admin._apps:
        cred_path = os.getenv("FIREBASE_CREDENTIALS_PATH")
        try:
            if cred_path and os.path.exists(cred_path):
                logger.info(f"Initializing Firebase Admin with credentials at {cred_path}")
                cred = credentials.Certificate(cred_path)
                firebase_admin.initialize_app(cred)
            else:
                project_id = os.getenv("FIREBASE_PROJECT_ID")
                
                # Check if ADC actually exists before initializing (since initialize_app is lazy)
                import google.auth
                from google.auth.exceptions import DefaultCredentialsError
                import google.auth.credentials
                
                has_adc = False
                try:
                    google.auth.default()
                    has_adc = True
                except DefaultCredentialsError:
                    pass

                if has_adc:
                    logger.info(f"Initializing Firebase Admin with Application Default Credentials (ADC) for project '{project_id}'")
                    if project_id:
                        firebase_admin.initialize_app(options={'projectId': project_id})
                    else:
                        firebase_admin.initialize_app()
                else:
                    logger.info("ADC not found. Initializing Firebase Admin with MockCredentials for local token verification.")
                    class MockCredentials(google.auth.credentials.Credentials):
                        def refresh(self, request): pass
                    firebase_admin.initialize_app(MockCredentials(), options={'projectId': project_id})
                    
        except Exception as e:
            logger.error(f"Failed to initialize Firebase Admin SDK: {e}")
            # We don't raise here immediately to allow tests to mock verification later, 
            # but auth will fail if real verification is attempted without proper init.

def verify_id_token(token: str) -> Optional[Dict[str, Any]]:
    """
    Verify the Firebase ID token and return the decoded user information.
    Raises ValueError or firebase_admin.auth.InvalidIdTokenError if invalid.
    """
    try:
        # Initialize if this is the first call
        initialize_firebase()
        
        decoded_token = auth.verify_id_token(token)
        return decoded_token
    except Exception as e:
        logger.warning(f"Firebase token verification failed: {e}")
        return None
